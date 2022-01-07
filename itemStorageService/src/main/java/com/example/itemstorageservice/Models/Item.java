package com.example.itemstorageservice.Models;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "items")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@NoArgsConstructor
public class Item {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @NotNull
    @Column(name = "serial", nullable = false)
    @Getter
    @Setter
    Long serial;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIdentityReference(alwaysAsId = true)
    @JoinColumn(name = "brand_id", nullable = false)
    @Getter
    @Setter
    BrandName brandName;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentItem", cascade = CascadeType.ALL)
    Set<Item> childItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIdentityReference(alwaysAsId = true)
    @Getter
    @Setter
    Item parentItem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    ItemType item_type;

    @Column(name = "creationdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Getter
    @Setter
    LocalDateTime creationDate;

    public Item(Long serial, BrandName brandName, Item parentItem, ItemType item_type) {
        this.serial = serial;
        this.brandName = brandName;
        this.item_type = item_type;
        this.parentItem = parentItem;
    }

    @JsonManagedReference
    public Set<Item> getChildItems() {
        return childItems;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setChildItems(Set<Item> childItems) {
        this.childItems = childItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
