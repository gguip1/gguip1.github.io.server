package io.github.gguip1.IntegratedServer.seasons.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDto {
    private String type;
    private String season;
    private String timestamp;
}
