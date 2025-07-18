package it.andrea.start.searchcriteria.user;

import java.io.Serial;
import java.io.Serializable;

import it.andrea.start.constants.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCriteria implements Serializable {

    @Serial
    private static final long serialVersionUID = -1762776686343092190L;

    private Long id;
    private String username;
    private String textSearch;
    private UserStatus userStatus;

}
