FROM adoptopenjdk/openjdk11-openj9
ADD web /opt/web
CMD ["/opt/web"]