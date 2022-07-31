package br.dev.schirmer.ddd.kernel.application.pipeline

interface Command<out TResult> : Request<TResult>