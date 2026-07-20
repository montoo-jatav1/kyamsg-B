package com.sandeshx.config

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.http.Method
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import java.util.concurrent.TimeUnit

object MinioFactory {
    const val BUCKET = "sandeshx-media"

    val client: MinioClient by lazy {
        MinioClient.builder()
            .endpoint(System.getenv("MINIO_ENDPOINT") ?: "http://localhost:9000")
            .credentials(
                System.getenv("MINIO_ACCESS_KEY") ?: "sandeshx",
                System.getenv("MINIO_SECRET_KEY") ?: "sandeshx-secret"
            )
            .build()
    }

    fun ensureBucket() {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build())
        }
    }

    /** Presigned PUT URL — client uploads the image directly to MinIO, backend never touches image bytes. */
    fun presignedUploadUrl(objectKey: String): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(BUCKET)
                .`object`(objectKey)
                .expiry(10, TimeUnit.MINUTES)
                .build()
        )

    fun presignedDownloadUrl(objectKey: String): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(BUCKET)
                .`object`(objectKey)
                .expiry(1, TimeUnit.HOURS)
                .build()
        )
}
