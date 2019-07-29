#
# The MIT License
# Copyright © 2010 JmxTrans team
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

FROM openjdk:12-alpine

RUN apk update \
   && apk add curl \
   && apk add libc6-compat \
   && apk add bash

ENV JMXMETRICS_HOME /usr/share/jmxmetrics
ENV PATH $JMXMETRICS_HOME/bin:$PATH
ENV JAR_FILE $JMXMETRICS_HOME/lib/jmxmetrics-all.jar
ENV HEAP_SIZE 512
ENV PERM_SIZE 384
ENV MAX_PERM_SIZE 384
ENV CONTINUE_ON_ERROR false
ENV CONF_DIR /var/lib/jmxmetrics

RUN addgroup jmxmetrics \
   && adduser jmxmetrics -s /bin/bash -h /usr/share/jmxmetrics -S -D -G jmxmetrics

WORKDIR ${JMXMETRICS_HOME}
RUN mkdir -p ${JMXMETRICS_HOME}/conf

COPY logback.xml ${JMXMETRICS_HOME}/conf/logback.xml

RUN mkdir -p ${JMXMETRICS_HOME}/lib/ \
    && JMXMETRICS_VERSION=`curl https://artifactory.provectus.com/artifactory/libs-release/com/provectus/jmxmetrics/maven-metadata.xml | sed -n 's:.*<release>\(.*\)</release>.*:\1:p'` \
    && mkdir -p /var/log/jmxmetrics \
    && wget -q https://artifactory.provectus.com/artifactory/libs-release/com/provectus/jmxmetrics/${JMXMETRICS_VERSION}/jmxmetrics-${JMXMETRICS_VERSION}-jar-with-dependencies.jar \
    && mv jmxmetrics-${JMXMETRICS_VERSION}-jar-with-dependencies.jar ${JAR_FILE}

COPY docker-entrypoint.sh /

EXPOSE 9999

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["start-with-jmx"]