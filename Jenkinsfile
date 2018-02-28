@Library('pipelines-shared-libs')
import com.gooddata.pipeline.Pipeline

def config = [
        'microservices': [
                'cfal-restapi': [
                        'build'     : [
                                'env'       : 'mvnw',
                                'path'      : '.',
                                'args'      : 'clean package -Pk8s -am -pl cfal-restapi/'
                        ],
                        'docker'    : [
                                'dockerfile': 'cfal-restapi/Dockerfile'
                        ],
                        'chartPath' : 'cfal.restapi.image'
                ],
        ],
        'charts'       : [
                'cfal'
        ]
]

Pipeline.get(this, config, [:]).run()
