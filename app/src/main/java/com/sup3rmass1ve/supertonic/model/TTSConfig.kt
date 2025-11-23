package com.sup3rmass1ve.supertonic.model

data class TTSConfig(
    val ae: AEConfig,
    val ttl: TTLConfig
)

data class AEConfig(
    val sample_rate: Int,
    val base_chunk_size: Int
)

data class TTLConfig(
    val latent_dim: Int,
    val chunk_compress_factor: Int
)
