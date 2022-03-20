package de.ma_vin.ape.users.service.context;

import de.ma_vin.ape.users.model.gen.dao.IIdentifiableDao;
import de.ma_vin.ape.users.service.AbstractRepositoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;

@Data
@AllArgsConstructor
public class RepositoryServiceContext<T extends IIdentifiableDao> {
    private String identification;
    private String className;
    private String prefix;
    private JpaRepository<T, Long> repository;
    private AbstractRepositoryService.DaoCreator<T> creator;
}
