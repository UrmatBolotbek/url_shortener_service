package faang.school.urlshortenerservice.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "app.url.life-time")
public class UrlLifeTimeConfig {
    private final int months;
    private final int days;
    private final int hours;

    @ConstructorBinding
    public UrlLifeTimeConfig(int months, int days, int hours) {
        this.months = months;
        this.days = days;
        this.hours = hours;
    }
}
