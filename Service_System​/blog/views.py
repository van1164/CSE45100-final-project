from django.shortcuts import redirect, render 
from django.utils import timezone
from blog.forms import PostForm 
from .models import Post
from django.shortcuts import render, get_object_or_404
from rest_framework import viewsets
from .models import Post
from .serializers import PostSerializer
import logging
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework import status
logger = logging.getLogger('myapp')


class BlogImages(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer

    def create(self, request, *args, **kwargs):
        print(request)
        data = {
            "author": request.user.id,
            "title": request.data.get('title'),
            "text": request.data.get('text'),
            "image": request.FILES.get('image'),
            "created_date": timezone.now(),
            "published_date": timezone.now()
        }
        serializer = PostSerializer(data=data)
        print(request.user.id)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    @action(detail=True, methods=['post'])
    def like(self, request, pk=None):
        post = self.get_object()
        post.likes += 1  # Increment likes by 1
        post.save()
        return Response({'likes': post.likes}, status=status.HTTP_200_OK)


def post_list(request):
    posts = Post.objects.filter().order_by('published_date') 
    return render(request, 'blog/post_list.html', {'posts': posts})
def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {'post': post})

def post_new(request):
    print(request)
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm()
    return render(request, 'blog/post_edit.html', {'form': form})

def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm(instance=post)
        return render(request, 'blog/post_edit.html', {'form': form})