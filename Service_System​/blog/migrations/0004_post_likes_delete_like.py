# Generated by Django 5.1.2 on 2024-11-05 13:58

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('blog', '0003_like'),
    ]

    operations = [
        migrations.AddField(
            model_name='post',
            name='likes',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.DeleteModel(
            name='Like',
        ),
    ]
