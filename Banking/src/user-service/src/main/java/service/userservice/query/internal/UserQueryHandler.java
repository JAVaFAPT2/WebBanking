package service.userservice.query.internal;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.userservice.query.api.GetUserQuery;
import service.userservice.query.internal.models.UserReadModel;
import service.userservice.repository.UserReadRepository;

@Service
public class UserQueryHandler {

    private final UserReadRepository userReadRepository;

    public UserQueryHandler(UserReadRepository userReadRepository) {
        this.userReadRepository = userReadRepository;
    }

    public Mono<UserReadModel> handleGetUser(GetUserQuery query) {
        return Mono.justOrEmpty(userReadRepository.findById(query.getUserId()))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
}
