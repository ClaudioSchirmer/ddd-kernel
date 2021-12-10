package dev.cschirmer.ddd.kernel.application.pipeline

interface Query<out TResult> : Request<TResult>