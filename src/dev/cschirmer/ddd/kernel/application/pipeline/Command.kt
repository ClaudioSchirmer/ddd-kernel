package dev.cschirmer.ddd.kernel.application.pipeline

interface Command<out TResult> : Request<TResult>