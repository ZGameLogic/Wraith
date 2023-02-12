package data.database.atlassian;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface AtlassianRepository extends JpaRepository<AtlassianConfig, Long> {
}
