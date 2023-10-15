package data.database.github.workflow;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "git_workflows")
public class Workflow {

    @Id
    private long id;
    private long textChannelId;
    private long messageId;
}
