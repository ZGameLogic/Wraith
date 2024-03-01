package data.api.github.payloads;

import data.api.github.Tree;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@Service
@NoArgsConstructor
public class TreePayload {
    private String sha;
    private String url;
    private boolean truncated;
    private List<Tree> tree;
}
