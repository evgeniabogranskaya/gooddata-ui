FROM mongo
COPY init.json /init.json
CMD mongoimport --host mongo --db cfal --collection cfal_default --type json --file /init.json --jsonArray
