FROM adoptopenjdk/openjdk16-openj9
ADD ./build/libs/web /opt/web
CMD ["/opt/web"]