package com.marketPlace.Orders.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "main_page_statistic")
@NoArgsConstructor
@Getter
@Setter
public class PageStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "openpagedate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime openPageDate;

    public PageStatistic(LocalDateTime pageopenTime){
        this.openPageDate = pageopenTime;
    }
}