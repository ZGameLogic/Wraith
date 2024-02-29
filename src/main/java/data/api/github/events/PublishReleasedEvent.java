package data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import data.api.github.Release;
import data.api.github.Repository;
import data.api.github.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishReleasedEvent {
    private Release release;
    private Repository repository;
    private User sender;
}
