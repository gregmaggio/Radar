FROM tomcat:8.5-jdk8-temurin
LABEL maintainer="gregorymaggio@gmail.com"

RUN rm -rf /usr/local/tomcat/webapps/ROOT
RUN rm -rf /usr/local/tomcat/webapps/docs
RUN rm -rf /usr/local/tomcat/webapps/examples
RUN rm -rf /usr/local/tomcat/webapps/host-manager
RUN rm -rf /usr/local/tomcat/webapps/manager

ADD target/radar-1.0.0.war /usr/local/tomcat/webapps/ROOT.war

RUN jar xf /usr/local/tomcat/webapps/ROOT.war /usr/local/tomcat/webapps/ROOT

#RUN chmod +x /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/importfromjson.sh
#CMD ["/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/importfromjson.sh"]

EXPOSE 8080
CMD ["catalina.sh", "run"]
