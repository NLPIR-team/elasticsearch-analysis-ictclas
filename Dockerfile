FROM docker.elastic.co/elasticsearch/elasticsearch:8.7.1
MAINTAINER yangyaofei yangyaofei@gmail.com
#COPY --chmod=777 build/distributions/analysis-ictclas-0.3.0.zip /tmp/analysis-ictclas.zip
RUN --mount=type=bind,src=build/distributions/analysis-ictclas-0.3.0.zip,target=/tmp/analysis-ictclas.zip ./bin/elasticsearch-plugin install --batch file:///tmp/analysis-ictclas.zip