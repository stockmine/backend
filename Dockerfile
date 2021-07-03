FROM adoptopenjdk/openjdk12
ADD ./web /opt/web
CMD ["/opt/web"]