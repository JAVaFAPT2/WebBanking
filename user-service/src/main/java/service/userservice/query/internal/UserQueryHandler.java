package service.userservice.query.internal;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.userservice.query.api.GetUserQuery;
import service.userservice.query.internal.models.UserReadModel;

@Service
public class UserQueryHandler {

    private final UserReadRepository userReadRepository;

    public UserQueryHandler(UserReadRepository userReadRepository) {
        this.userReadRepository = userReadRepository;
    }

    public Mono<UserReadModel> handleGetUser(GetUserQuery query) {
        return Mono.fromCallable(() -> userReadRepository.findById(query.getUserId()))
                .flatMap(optionalUser -> {
                    if (optionalUser.isPresent()) {
                        return Mono.just(optionalUser.get());
                    } else {
                        return Mono.error(new RuntimeException("User not found"));
                    }
                });
    }
}