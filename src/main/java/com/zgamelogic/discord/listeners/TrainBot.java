package com.zgamelogic.discord.listeners;

import com.zgamelogic.data.metra.MetraRoute;
import com.zgamelogic.data.metra.MetraStop;
import com.zgamelogic.data.metra.api.TrainSearchResult;
import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordMapping;
import com.zgamelogic.discord.annotations.EventProperty;
import com.zgamelogic.discord.data.Model;
import com.zgamelogic.services.MetraService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@DiscordController
@AllArgsConstructor
public class TrainBot {
    private final MetraService metraService;

    @DiscordMapping(Id = "metra", FocusedOption = "route")
    public void metraRouteAutocomplete(CommandAutoCompleteInteractionEvent event, @EventProperty String route){
        event.replyChoices(
            metraService.getRoutes().stream()
                .filter(s -> s.getRouteLongName().toLowerCase().startsWith(route.toLowerCase()) || s.getRouteId().toLowerCase().startsWith(route.toLowerCase()))
                .sorted(Comparator.comparing(MetraRoute::getRouteLongName))
                .map(s -> new Command.Choice(s.getRouteLongName(), s.getRouteId())).toList()
        ).queue();
    }

    @DiscordMapping(Id = "metra", FocusedOption = "start")
    public void metraStartAutocomplete(
            CommandAutoCompleteInteractionEvent event,
            @EventProperty String route,
            @EventProperty String start
    ){
        event.replyChoices(
            metraService.getStopsOnRouteByRouteId(route, LocalDate.now()).stream()
                .filter(s -> s.getStopName().toLowerCase().startsWith(start.toLowerCase()) || s.getStopId().toLowerCase().startsWith(start.toLowerCase()))
                .map(s -> new Command.Choice(s.getStopName(), s.getStopId())).toList()
        ).queue();
    }

    @DiscordMapping(Id = "metra", FocusedOption = "end")
    public void metraEndAutocomplete(
            CommandAutoCompleteInteractionEvent event,
            @EventProperty String route,
            @EventProperty String end
    ){
        event.replyChoices(
            metraService.getStopsOnRouteByRouteId(route, LocalDate.now()).stream()
                .filter(s -> s.getStopName().toLowerCase().startsWith(end.toLowerCase()) || s.getStopId().toLowerCase().startsWith(end.toLowerCase()))
                .map(s -> new Command.Choice(s.getStopName(), s.getStopId())).toList()
        ).queue();
    }

    @DiscordMapping(Id = "metra", Event = SlashCommandInteractionEvent.class, Document = "metra")
    public void metraSlashCommand(
        @EventProperty String route,
        @EventProperty String start,
        @EventProperty String end,
        Model model
    ) {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        MetraRoute metraRoute = metraService.getRouteById(route);
        MetraStop metraStart = metraService.getStopById(start);
        MetraStop metraEnd = metraService.getStopById(end);
        List<TrainSearchResult> results = metraService.trainSearch(route, start, end, date, time);
        String resultString = results.stream()
            .map(result -> String.format("Train %s | %s %s", result.trainNumber(), result.depart(), result.arrive()))
            .collect(Collectors.joining("\n"));
        model.addContext("start", metraStart.getStopName());
        model.addContext("end", metraEnd.getStopName());
        model.addContext("color", "#" + metraRoute.getRouteColor());
        model.addContext("results", resultString);
    }

    @Bean
    public SlashCommandData trainSearch(){
        return Commands.slash("metra", "Get a list of train schedules for the metra trains.")
            .addOptions(
                new OptionData(OptionType.STRING, "route", "The line the train runs on.", true, true),
                new OptionData(OptionType.STRING, "start", "The station to start on.", true, true),
                new OptionData(OptionType.STRING, "end", "The station to end on.", true, true)
            );
    }
}
