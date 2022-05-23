package teacherbook.model.users;

import javax.persistence.*;

@Entity
public class Parent {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long parent_id;

    @OneToOne(mappedBy = "parent")
    private TeacherbookUser user;

}
