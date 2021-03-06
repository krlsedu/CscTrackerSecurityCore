package com.csctracker.securitycore.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConfigsDTO {
    private String favoriteContact;
    private String applicationNotify;
    private String timeZone;
}
