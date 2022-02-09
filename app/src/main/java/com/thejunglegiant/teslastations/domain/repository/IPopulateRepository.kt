package com.thejunglegiant.teslastations.domain.repository

interface IPopulateRepository {
    suspend fun initDb(): Boolean
}