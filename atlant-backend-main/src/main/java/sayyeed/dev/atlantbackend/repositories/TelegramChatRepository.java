package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.TelegramChatEntity;

import java.util.List;

public interface TelegramChatRepository extends CrudRepository<TelegramChatEntity, String> {

    List<TelegramChatEntity> findAllByOrderByCreatedAtDesc();

    List<TelegramChatEntity> findAllByActiveTrue();
}
