package com.dsgroup4.httphandler.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    @Query("SELECT r from Result r")
    List<Result> getAll();

    @Query("SELECT r from Result r where r.order_id =:order_id")
    List<Result> getResultById(@Param("order_id") String order_id);
}
