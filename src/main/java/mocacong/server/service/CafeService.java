package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.repository.CafeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;

    public void cafeSave(CafeRegisterRequest request) {
        Cafe cafe = new Cafe(request.getId(), request.getName());
        cafeRepository.findByMapId(request.getId())
                .ifPresentOrElse(
                        cafe1 -> {
                        },
                        () -> {
                            cafeRepository.save(cafe);
                        }
                );
    }
}
