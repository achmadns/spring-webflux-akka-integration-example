package id.co.alamisharia.simjam.repository;

import id.co.alamisharia.simjam.domain.Group;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends ReactiveSortingRepository<Group, Long> {
}
