package com.marketPlace.itemstorageservice.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "brands")
@NoArgsConstructor
@Getter
@Setter
public class BrandName {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(name = "brandname", unique = true)
    private String name;

    @Column(name = "creationdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @Column(name = "brandversion")
    private String version;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "brandName", cascade = CascadeType.ALL)
    private Set<Item> items = new HashSet<>();

    public BrandName(String name, String version){
        this.name = name;
        this.version = version;
    }
}

