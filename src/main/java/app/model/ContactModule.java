package app.model;


import lombok.Value;

import java.io.Serializable;

@Value
public class ContactModule implements Serializable {

    String name;

    String role;

    String phone;

    String phoneExt;

    String email;
}
