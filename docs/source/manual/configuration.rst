.. _man-configuration-metrics-datadog:

Datadog Reporter
------------

Reports metrics periodically to datadog.

Extends the attributes that are available to :ref:`formatted reporters <man-configuration-metrics-formatted>`

.. code-block:: yaml

    metrics:
      reporters:
        - type: datadog
          endpoint: https://app.datadoghq.com/api/v1/series
          apiKey: (none)


====================== =========================================  ===========
Name                   Default                                    Description
====================== =========================================  ===========
endpoint               https://app.datadoghq.com/api/v1/series    The API Endpoint for Datadog
apiKey                 No Default                                 The API Authorization Key



