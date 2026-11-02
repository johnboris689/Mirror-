FROM debian:bookworm-slim AS build
RUN apt-get update && apt-get install -y --no-install-recommends autoconf automake libtool make gcc g++ pkg-config zlib1g-dev libssl-dev libbrotli-dev libzstd-dev libpsl-dev ca-certificates git && rm -rf /var/lib/apt/lists/*
WORKDIR /src
COPY . .
RUN if [ ! -f src/coucal/coucal.c ]; then git clone --depth 1 https://github.com/xroche/coucal.git src/coucal; fi
RUN test -f src/coucal/coucal.c && ./bootstrap && ./configure --prefix=/usr --disable-dependency-tracking && make -j"$(nproc)" && make DESTDIR=/stage install

FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y --no-install-recommends libstdc++6 zlib1g libssl3 libbrotli1 libzstd1 libpsl5 ca-certificates && rm -rf /var/lib/apt/lists/*
COPY --from=build /stage/usr /usr
ENV HOME=/data
RUN mkdir -p /data && useradd -m -u 10001 mirror && chown -R mirror:mirror /data
USER mirror
EXPOSE 10000
CMD ["sh", "-c", "exec htsserver --bind 0.0.0.0 --port ${PORT:-10000}"]
