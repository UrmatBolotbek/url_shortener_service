package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.model.dto.UrlRequestDto;
import faang.school.urlshortenerservice.model.dto.UrlResponseDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/url")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UrlResponseDto shorten(@RequestBody @Valid UrlRequestDto requestDto) {
        return urlService.generateShortUrl(requestDto);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirect(@PathVariable String hash) {
        String longUrl = urlService.getUrlByHash(hash);
        return ResponseEntity.status(302)
                .header("Location", longUrl)
                .build();
    }
}
