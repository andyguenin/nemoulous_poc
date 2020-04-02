FROM continuumio/miniconda:4.7.12

ADD . nemoulous

ADD requirements.txt requirements.txt

ADD environment.yml environment.yml

RUN ["conda", "env", "create", "-f", "environment.yml"]

#RUN ["conda", "create", "-n", "nemoulous"]

#RUN ["conda", "install", "-n", "nemoulous", "pip"]

#RUN ["pip", "install", "-r", "requirements.txt"]

#CMD ["python", "nemoulous/loader/db_term_structure_loader.py"]
