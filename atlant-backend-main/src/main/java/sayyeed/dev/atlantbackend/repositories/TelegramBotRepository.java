package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.TelegramBotEntity;

import java.util.List;

public interface TelegramBotRepository extends CrudRepository<TelegramBotEntity, String> {

    List<TelegramBotEntity> findAllByOrderByCreatedAtDesc();

    List<TelegramBotEntity> findAllByActiveTrue();
}
