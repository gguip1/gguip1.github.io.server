package io.github.gguip1.IntegratedServer.seasons.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionCountDto {
    private String type;
    private int connectionCount;
    private String timestamp;
}
