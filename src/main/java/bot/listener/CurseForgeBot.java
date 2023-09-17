package bot.listener;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.jda.AdvancedListenerAdapter;
import data.database.curseforge.CurseforgeRecord;
import data.database.curseforge.CurseforgeRepository;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zgamelogic.jda.Annotations.*;

@Slf4j
@RestController
public class CurseForgeBot extends AdvancedListenerAdapter {

    private final CurseforgeRepository checks;
    private JDA bot;

    @Autowired
    public CurseForgeBot(CurseforgeRepository checks) {
        this.checks = checks;
        getCommands().add(Commands.slash("curseforge", "Slash command for curseforge related things")
                .addSubcommands(
                        new SubcommandData("listen", "Listens to a project")
                                .addOption(OptionType.STRING, "project", "Project to watch", true)
                                .addOption(OptionType.BOOLEAN, "mention", "Get mentioned when this updates", false),
                        new SubcommandData("forget", "Stops listening to a project")
                                .addOption(OptionType.STRING, "project", "Project to watch", true),
                        new SubcommandData("list", "Lists all the projects currently followed in this channel"),
                        new SubcommandData("updated", "Shows when the project was last updated")
                                .addOption(OptionType.STRING, "project", "Project to check", true, true)
                ));
    }

    @OnReady
    public void ready(ReadyEvent event) {
        update();
    }

    @AutoCompleteResponse(slashSubCommandId = "updated", slashCommandId = "curseforge", focusedOption = "project")
    private void autoCompleteProject(CommandAutoCompleteInteractionEvent event){
        LinkedList<String> projects = new LinkedList<>();
        for(CurseforgeRecord record: checks.getProjectsByGuildAndChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong())){
            projects.add(record.getName());
        }
        String[] words = projects.toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @SlashResponse(value = "curseforge", subCommandName = "list")
    private void list(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        LinkedList<CurseforgeProject> projects = new LinkedList<>();
        for(CurseforgeRecord record: checks.getProjectsByGuildAndChannel(event.getGuild().getIdLong(), event.getChannel().getIdLong())){
            projects.add(new CurseforgeProject(record.getProjectId()));
        }
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.curseforgeList(projects)).queue();
    }

    @SlashResponse(value = "curseforge", subCommandName = "updated")
    private void updated(SlashCommandInteractionEvent event){
        String projectName = event.getOption("project").getAsString();
        Optional<CurseforgeRecord> project = checks.getProjectByName(projectName);
        if(project.isPresent()){
            event.replyEmbeds(EmbedMessageGenerator.curseforgeUpdated(project.get())).queue();
        } else {
            event.reply("Cannot find a project with that name").setEphemeral(true).queue();
        }
    }

    @SlashResponse(value = "curseforge", subCommandName = "listen")
    private void follow(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        String project = event.getOption("project").getAsString();
        CurseforgeRecord cfr = new CurseforgeRecord();
        cfr.setChannelId(event.getChannel().getIdLong());
        cfr.setGuildId(event.getGuild().getIdLong());
        cfr.setLastChecked(new Date());
        cfr.setLastUpdated(new Date());
        cfr.setMentionable(
                event.getOption("mention") != null && event.getOption("mention").getAsBoolean()
        );
        cfr.setProjectId(project);
        CurseforgeProject response = new CurseforgeProject(project);
        if(!response.isValid()){
            event.getHook().sendMessage("No project with that ID found").queue();
            return;
        }
        cfr.setName(response.name);
        cfr.setProjectVersionId(response.fileId);
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.curseforgeInitial(response)).queue();
        checks.save(cfr);
    }

    @SlashResponse(value = "curseforge", subCommandName = "forget")
    private void forget(SlashCommandInteractionEvent event){
        String project = event.getOption("project").getAsString();
        Optional<CurseforgeRecord> dbProject = checks.getProjectById(project, event.getGuild().getIdLong(), event.getChannel().getIdLong());
        if(dbProject.isPresent()){
            checks.deleteById(dbProject.get().getId());
            event.reply("No longer watching this project").queue();
            return;
        }
        event.reply("No project with that ID is being followed").queue();
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        update();
    }


    public void update(){
        for(CurseforgeRecord check: checks.findAll()){
            CurseforgeProject current = new CurseforgeProject(check.getProjectId());
            if(current.getFileId() != null && (check.getProjectVersionId() == null || !check.getProjectVersionId().equals(current.fileId))){
                Boolean mention = check.getMentionable();
                MessageCreateAction message = bot.getGuildById(check.getGuildId()).getTextChannelById(check.getChannelId()).sendMessageEmbeds(
                    EmbedMessageGenerator.curseforgeUpdate(current, mention)
                );
                if(mention != null && mention) message.mentionUsers(232675572772372481L);
                message.queue();
                check.setProjectVersionId(current.getFileId());
                check.setLastUpdated(new Date());
                check.setName(current.getName());
                log.info("Project: " + check.getProjectId() + "\tOld file: " + check.getProjectVersionId() + "\tNew file: " + current.fileId);
            }
            check.setLastChecked(new Date());
            check.setName(current.getName());
            checks.save(check);
        }
    }

    @Getter
    @ToString
    public class CurseforgeProject {
        private String name;
        private String summary;
        private String downloadCount;
        private String logoUrl;
        private String url;
        private String fileId;
        private String fileName;
        private String serverFileUrl, serverFileName;
        private boolean valid;

        public CurseforgeProject(String project){
            serverFileUrl = "";
            serverFileName = "";
            String url = "https://api.curseforge.com/v1/mods/" + project;
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("x-api-key", App.config.getCurseforgeApiToken());
            JSONObject json;
            try {
                HttpResponse httpresponse = httpclient.execute(httpget);
                if (httpresponse.getStatusLine().getStatusCode() != 200) return;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
                json = new JSONObject(in.readLine());
            } catch (IOException | JSONException e) {
                log.error("error", e);
                return;
            }

            try {
                name = json.getJSONObject("data").getString("name");
                summary = json.getJSONObject("data").getString("summary");
                downloadCount = json.getJSONObject("data").getString("downloadCount");
                logoUrl = json.getJSONObject("data").getJSONObject("logo").getString("url");
                this.url = json.getJSONObject("data").getJSONObject("links").getString("websiteUrl");
                fileId = json.getJSONObject("data").getString("mainFileId");
                JSONArray files = json.getJSONObject("data").getJSONArray("latestFiles");
                for(int i = 0; i < files.length(); i++){
                    JSONObject file = files.getJSONObject(i);
                    if(file.getLong("id") == Long.parseLong(fileId)){
                        fileName = file.getString("displayName");
                        if(file.has("serverPackFileId")){
                            getServerFile(project, file.getString("serverPackFileId"));
                        }
                        break;
                    }
                }
            } catch (JSONException ignored) {
            }
            valid = true;
        }

        private void getServerFile(String project, String file) throws JSONException {
            String url = "https://api.curseforge.com/v1/mods/" + project + "/files/" + file;
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("x-api-key", App.config.getCurseforgeApiToken());
            JSONObject json;
            try {
                HttpResponse httpresponse = httpclient.execute(httpget);
                if (httpresponse.getStatusLine().getStatusCode() != 200) return;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
                json = new JSONObject(in.readLine());
            } catch (IOException | JSONException e) {
                return;
            }

            serverFileName = json.getJSONObject("data").getString("displayName");
            serverFileUrl = json.getJSONObject("data").getString("downloadUrl");
        }
    }
}
