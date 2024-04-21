package br.dev.schirmer.ddd.kernel.application.pipeline

interface Query<out TResult> : Request<TResult>