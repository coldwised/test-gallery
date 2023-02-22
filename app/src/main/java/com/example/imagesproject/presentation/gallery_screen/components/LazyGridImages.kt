package com.example.imagesproject.presentation.gallery_screen.components

import com.example.imagesproject.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.imagesproject.presentation.gallery_screen.ui_events.GalleryScreenEvent
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LazyGridImages(
    lazyGridState: LazyGridState,
    imagesUrlList: ImmutableList<String>,
    onGalleryScreenEvent: (GalleryScreenEvent) -> Unit,
) {
    val notValidImageIndexes = mutableListOf<Int>()
    val context = LocalContext.current
    val gridItemModifier = Modifier
        .padding(1.dp)
        .fillMaxWidth()
        .aspectRatio(1f)

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
        ,
        state = lazyGridState,
        columns = GridCells.Adaptive(90.dp)
    ) {
        items(imagesUrlList.size) { index ->
            val imageUrl = imagesUrlList[index]
            var isSuccess by rememberSaveable {
                mutableStateOf(true)
            }
            val contentScale = ContentScale.Crop
            val painter = rememberAsyncImagePainter(
                model = ImageRequest
                    .Builder(context)
                    .data(imageUrl)
                    .size(coil.size.Size.ORIGINAL)
                    .error(R.drawable.image_not_found)
                    .placeholder(
                        if(isSuccess)
                            R.drawable.placeholder else {
                            R.drawable.image_not_found
                        }
                    )
                    .build(),
                filterQuality = FilterQuality.None,
                contentScale = contentScale,
                onState = { imageState ->
                    when(imageState) {
                        is AsyncImagePainter.State.Error -> {
                            notValidImageIndexes.add(index)
                            isSuccess = false
                        }
                        is AsyncImagePainter.State.Success -> {
                            isSuccess = true
                        }
                        else -> {}
                    }
                }
            )
            Image(
                modifier = gridItemModifier
                    .clickable(
                        enabled = isSuccess,
                        onClick = {
                            val size = painter.intrinsicSize
                            val visibleItems =
                                lazyGridState.layoutInfo.visibleItemsInfo
                            val lastElement = visibleItems.last()
                            val currentGridItemOffset =
                                visibleItems.find { it.index == index }?.offset ?: IntOffset.Zero
                            val offset =
                                lazyGridState.layoutInfo.viewportSize.height - lastElement.size.height
                            onGalleryScreenEvent(GalleryScreenEvent.OnSavePainterIntrinsicSize(size))
                            onGalleryScreenEvent(
                                GalleryScreenEvent.OnSaveGridItemOffsetToScroll(
                                    offset
                                )
                            )
                            onGalleryScreenEvent(
                                GalleryScreenEvent.OnSaveCurrentGridItemOffset(
                                    currentGridItemOffset
                                )
                            )
                            for (i in notValidImageIndexes) {
                                onGalleryScreenEvent(GalleryScreenEvent.OnSaveNotValidImageIndex(i))
                            }
                            onGalleryScreenEvent(GalleryScreenEvent.OnImageClick(index))
                        }
                    ),
                painter = painter,
                contentScale = contentScale,
                contentDescription = null,
            )
        }
    }
}