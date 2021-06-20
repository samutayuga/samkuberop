# Build docker image using the secured image from dockerhub.gemalto.com
FROM adoptopenjdk/openjdk11

RUN mkdir -p /app/lib
RUN mkdir -p /app/bin
RUN mkdir -p /app/etc
RUN mkdir -p /app/etc/kubeinspect


ENV APP_LIB="/app/lib"
ENV APP_BIN="/app/bin"
ENV APP_ETC="/app/etc"
RUN export PATH=$APP_BIN:$PATH
# Copy libraries...
#COPY ./context/jars $APP_LIB/atmpoc

# Copy application...
COPY ./jars/kubeinspect.jar $APP_LIB

# Copy script for running application...
COPY bin/start_kubeinspect.sh /

# Copy config
COPY ./kubeinspect $APP_ETC/kubeinspect

# Add permissions...
RUN chmod -R 777 /start_kubeinspect.sh
RUN ls $APP_BIN
# Define entry point...
ENTRYPOINT ["/start_kubeinspect.sh"]
CMD [""]

