package data.database.atlassian;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Atlassian")
public class AtlassianConfig {

    @Id
    private long id;
    private Long threadChannelId;

    public AtlassianConfig(long id){
        this.id = id;
    }
}
