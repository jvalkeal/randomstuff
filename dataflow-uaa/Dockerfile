FROM tomcat:8-jre8-alpine

ENV UAA_CONFIG_PATH $CATALINA_HOME
ENV CATALINA_OPTS="-Xmx800m"

RUN rm -fr $CATALINA_HOME/webapps/ROOT
RUN rm -f $CATALINA_HOME/webapps/ROOT.war
ADD cloudfoundry-identity-uaa.war $CATALINA_HOME/webapps/uaa.war
ADD uaa.yml $CATALINA_HOME/uaa.yml

EXPOSE 8080

CMD ["catalina.sh", "run"]

