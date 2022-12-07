package study.datajpa.dto;

import lombok.Getter;

@Getter
public class UsernameOnlyDTO {
    private final String username;

    public UsernameOnlyDTO(String username) {
        this.username = username;
    }
}
