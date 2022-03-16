package com.marketplace.itemstorageservice.models;

import com.fasterxml.jackson.annotation.*;
import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NamedNativeQueries
        ({
                @NamedNativeQuery(
                        name = "findAllItemsDTO",
                        query = """
                     with t2 as (select id, parent_id, item_type from items)
                     select t1.id as parent_id, t1.serial, count(case when t2.item_type='ITEM' then t2.parent_id end) as children,
                     b.brandname, b.brandversion from items as t1 left join t2 on t1.id=t2.parent_id left join brands as b on t1.brand_id=b.id
                     where t1.item_type='PACK' group by t1.id, t1.serial, b.brandname, b.brandversion
                    """,
                        resultSetMapping = "DTOModels.ItemDetailedInfoDTO"),

                @NamedNativeQuery(
                        name = "getItemDTOByParentId",
                        query = """
                     with t2 as (select id, parent_id, item_type from items)
                     select t1.id as parent_id, t1.serial, count(case when t2.item_type='ITEM' then t2.parent_id end) as children,
                     b.brandname, b.brandversion from items as t1 left join t2 on t1.id=t2.parent_id left join brands as b on t1.brand_id=b.id
                     where t1.id=:parent_id and t1.item_type='PACK' group by t1.id, t1.serial, b.brandname, b.brandversion            
                     """,
                        resultSetMapping = "DTOModels.ItemDetailedInfoDTO"),

                @NamedNativeQuery(
                        name = "removeItemsFromPackage",
                        query = """
                     with childrenTable as
                     (select id, row_number() over (order by id) as child_row_number from items where item_type='ITEM' and parent_id=:parent_id)
                     delete from items where id in
                     (select id from childrenTable limit :items_count) and (select max(child_row_number) from childrenTable)>=:items_count        
                     """)
        })

@SqlResultSetMapping(name="DTOModels.ItemDetailedInfoDTO",
        classes = @ConstructorResult(
                targetClass = ItemDetailedInfoDTO.class,
                columns = {
                        @ColumnResult(name = "parent_id", type = Long.class),
                        @ColumnResult(name = "serial", type = Long.class),
                        @ColumnResult(name = "children", type = Long.class),
                        @ColumnResult(name = "brandname", type = String.class),
                        @ColumnResult(name = "brandversion", type = String.class)
                }))

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
