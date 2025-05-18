package app.tracktune.model.author;

import java.util.Objects;

public class Author {
    private final Integer id;
    private final String authorshipName;
    private final AuthorStatusEnum status;

    public Author(Integer id, String authorshipName, AuthorStatusEnum status) {
        this.id = id;
        this.authorshipName = authorshipName;
        this.status = status != null ? status : AuthorStatusEnum.ACTIVE;
    }

    public Author(String authorshipName, AuthorStatusEnum status) {
        this(null, authorshipName, status);
    }

    public Integer getId() {
        return id;
    }

    public String getAuthorshipName() {
        return authorshipName;
    }

    public AuthorStatusEnum getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return authorshipName;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if(o instanceof Author a){
            result = a.getAuthorshipName().equalsIgnoreCase(this.authorshipName);
        }
        return result;
    }
}