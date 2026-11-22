package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.CarouselEntity;

import java.util.List;

public interface CarouselRepository extends CrudRepository<CarouselEntity, String> {

    List<CarouselEntity> findAllByActiveTrueOrderByCreatedAtDesc();

    List<CarouselEntity> findAllByOrderByCreatedAtDesc();
}
