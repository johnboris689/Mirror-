# Deployment

The original `htsserver` is a local desktop-oriented HTTP server. It serves the original WebHTTrack HTML interface and launches the original mirroring engine. Hosting it as a public Render service is not equivalent to the desktop workflow: persistent project storage, process supervision, user isolation, authentication, resource limits, and secure multi-user execution must be designed before exposing it to the public internet. The included Docker/Render files are a build-and-start experiment, not a production-safe multi-user service.
