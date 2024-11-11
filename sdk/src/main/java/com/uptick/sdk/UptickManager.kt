package com.uptick.sdk

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import coil.load
import com.uptick.sdk.model.Placement
import com.uptick.sdk.model.UptickResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class UptickManager {
    private val network by lazy {
        Network().apiService
    }
    private var integrationId = ""
    private var flowId = ""
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
    private var scope = CoroutineScope(coroutineExceptionHandler)
    private var primaryColor = Color.parseColor("#5bb85d")
    private var secondaryColor = Color.parseColor("#efefef")
    private var bgColor = Color.parseColor("#4D000000")
    private val lightGrey = Color.parseColor("#909090")
    private var placement = Placement.ORDER_CONFIRMATION
    private var optionalParams = mutableMapOf<String, String>()
    var onError: (String) -> Unit = {}

    fun setPrimaryColor(@ColorInt color: Int) {
        primaryColor = color
    }

    fun setSecondaryColor(@ColorInt color: Int) {
        secondaryColor = color
    }

    fun setBgColor(@ColorInt color: Int) {
        bgColor = color
    }

    private var container: FrameLayout? = null
    private var context: Context? = null
    fun initiateView(
        context: Context,
        container: FrameLayout,
        integrationId: String,
        placement: Placement = Placement.ORDER_CONFIRMATION,
        optionalParams: Map<String, String> = mapOf()
    ) {
        this.context = context
        this.container = container
        this.integrationId = integrationId
        this.placement = placement
        this.optionalParams = optionalParams.toMutableMap()

        scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val flow = network.newFlow(
                integrationId, this@UptickManager.placement.value,
                optionalParams
            )
            if (flow.isSuccessful) {
                flow.body()?.data?.let { response ->
                    response.find { it.type == "flow" }?.let {
                        if (it.personalization == false)
                            this@UptickManager.optionalParams.remove("first_name")
                        flowId = it.id
                        handleNextOffer(flow.body()?.links?.nextOffer)
                    }
                }
            } else {
                flow.errorBody()?.let {
                    try {
                        handleError(JSONObject(it.string()).getString("error"))
                    } catch (e: Exception) {
                        e.localizedMessage?.let { message ->
                            handleError(message)
                        }
                    }
                }
            }
        }
    }

    private fun showOffer(url: String) {
        scope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val response =
                network.nextOffer(url = url, placement = placement.value, options = optionalParams)
            if (response.isSuccessful) response.body()?.let {
                showOfferView(it)
            } else {
                response.errorBody()?.let {
                    try {
                        handleError(
                            JSONObject(it.string()).getJSONArray("errors").getJSONObject(0)
                                .getString("title")
                        )
                    } catch (e: Exception) {
                        e.localizedMessage?.let { message ->
                            handleError(message)
                        }
                    }
                }
            }
        }
    }

    private fun handleNextOffer(url: String?) {
        url?.let {
            showOffer(it)
        } ?: kotlin.run {
            container?.removeAllViews()
        }
    }

    private fun handleError(error: String) {
        scope.launch(Dispatchers.Main + coroutineExceptionHandler) {
            onError(error)
        }
    }

    private fun showOfferView(response: UptickResponse) {
        scope.launch(Dispatchers.Main + coroutineExceptionHandler) {
            response.data.find { it.type == "offer" }?.attributes?.let { offer ->
                val isTablet = isTablet(context!!)
                val horizontalPadding = horizontalPadding(isTablet)
                val parentContainer = FrameLayout(context!!).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(if (placement == Placement.ORDER_CONFIRMATION) bgColor else Color.TRANSPARENT)
                }
                val cardView = CardView(context!!).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity =
                            if (placement == Placement.ORDER_CONFIRMATION) Gravity.CENTER else Gravity.TOP
                        setMargins(
                            if (placement == Placement.ORDER_CONFIRMATION) 16.dpToPx() else 0,
                            0,
                            if (placement == Placement.ORDER_CONFIRMATION) 16.dpToPx() else 0,
                            0
                        )
                    }
                    cardElevation =
                        if (placement == Placement.ORDER_CONFIRMATION) 8.dpToPx().toFloat() else 0f
                    setCardBackgroundColor(if (placement == Placement.ORDER_CONFIRMATION) Color.WHITE else Color.TRANSPARENT)
                }
                val linearLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                cardView.addView(linearLayout)
                parentContainer.addView(cardView)

                // Header
                offer.header?.forEach {
                    if (it.type == "text") {
                        val headerTextView = TextView(context).apply {
                            includeFontPadding = false
                            text = it.text
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: Color.WHITE)
                            setTextStyle(it.attributes?.emphasis)
                            setBackgroundColor(primaryColor)
                            setPadding(
                                horizontalPadding,
                                16.dpToPx(),
                                horizontalPadding,
                                16.dpToPx()
                            )
                        }
                        linearLayout.addView(headerTextView)
                    }
                }
                // Digits
                offer.offers?.let { offerDigits ->
                    val digitsContainer = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.gravity = Gravity.CENTER
                        params.setMargins(0, 16.dpToPx(), 0, 16.dpToPx())
                        layoutParams = params
                        dividerDrawable = GradientDrawable().apply {
                            setSize(8.dpToPx(), 8.dpToPx())
                        }
                        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                    }
                    for (i in offerDigits.start..offerDigits.size) {
                        val digit = TextView(context).apply {
                            width = 32.dpToPx()
                            height = 32.dpToPx()
                            text = i.toString()
                            textSize = 12f
                            setTextColor(Color.WHITE)
                            background =
                                createCircle(if (offerDigits.current >= i) primaryColor else secondaryColor)
                            gravity = Gravity.CENTER
                        }
                        digitsContainer.addView(digit)
                    }
                    linearLayout.addView(digitsContainer)
                }
                //image
                var imageView: ImageView? = null
                offer.image?.let { image ->
                    imageView = ImageView(context).apply {
                        load(image.url)
                        scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        setPadding(horizontalPadding, 8.dpToPx(), horizontalPadding, 8.dpToPx())
                    }

                    //linearLayout.addView(imageView, params)
                }
                //personalization
                offer.personalization?.forEach {
                    if (it.type == "text") {
                        val disclaimerTextView = TextView(context).apply {
                            gravity = Gravity.START
                            includeFontPadding = false
                            setTextColor(
                                getTextColor(it.attributes?.appearance)
                                    ?: Color.parseColor("#191919")
                            )
                            text = it.text
                            setTextSize(it.attributes?.size)
                            setTextStyle(it.attributes?.emphasis)
                            setPadding(horizontalPadding, 8.dpToPx(), horizontalPadding, 8.dpToPx())
                        }
                        linearLayout.addView(disclaimerTextView)
                    }
                }

                val contentContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = if (isTablet) LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ) else LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                //sponsored
                offer.sponsored?.forEach {
                    if (it.type == "text") {
                        val sponsoredTextView = TextView(context).apply {
                            includeFontPadding = false
                            text = it.text
                            setPadding(horizontalPadding, 8.dpToPx(), horizontalPadding, 8.dpToPx())
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: lightGrey)
                            setTextStyle(it.attributes?.emphasis)
                        }
                        contentContainer.addView(sponsoredTextView)
                    }
                }

                // content
                var contentString: CharSequence? = null
                offer.content?.forEach {
                    if (it.type == "text") {
                        val mText = it.text
                        val text = android.text.SpannableString(mText).apply {
                            if (it.attributes?.emphasis == "bold") boldSpan(mText)
                        }
                        contentString = android.text.TextUtils.concat(contentString ?: "", text)
                    }
                }
                contentString?.let {
                    val contentTextView = TextView(context).apply {
                        includeFontPadding = false
                        minHeight = 0
                        text = contentString
                        textSize = 16f
                        setTextColor(Color.parseColor("#191919"))
                        setPadding(horizontalPadding, 8.dpToPx(), horizontalPadding, 8.dpToPx())
                    }
                    contentContainer.addView(contentTextView)
                }

                // Actions
                val actionsContainer = LinearLayout(context).apply {
                    orientation = if (isTablet) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    dividerDrawable = GradientDrawable().apply {
                        setSize(16.dpToPx(), 16.dpToPx())
                    }
                    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                    setPadding(horizontalPadding, 8.dpToPx(), horizontalPadding, 8.dpToPx())
                }
                offer.actions?.let {
                    it.forEach { item ->
                        if (item.type == "button") {
                            val button = android.widget.Button(context).apply {
                                text = item.text
                                setBackgroundColor(if (item.attributes?.kind == "primary") primaryColor else secondaryColor)
                                setTextColor(
                                    if (item.attributes?.kind == "primary") Color.WHITE else Color.parseColor(
                                        "#191919"
                                    )
                                )
                                setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                                setOnClickListener { buttonView ->
                                    if (item.attributes?.kind == "primary") {
                                        item.attributes.to?.let { link ->
                                            buttonView.isEnabled = false
                                            handleNextOffer(response.links.nextOffer)
                                            context.openLink(link)
                                        }
                                    } else {
                                        buttonView.isEnabled = false
                                        handleNextOffer(response.links.nextOffer)
                                    }
                                }
                            }
                            val params = LinearLayout.LayoutParams(
                                if (isTablet) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            actionsContainer.addView(button, params)
                        }
                    }
                    contentContainer.addView(actionsContainer)
                }

                // Disclaimer
                offer.disclaimer?.forEach {
                    if (it.type == "text") {
                        val disclaimerTextView = TextView(context).apply {
                            gravity = Gravity.START
                            includeFontPadding = false

                            text = it.text
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: Color.GRAY)
                            setTextStyle(it.attributes?.emphasis)
                            setPadding(horizontalPadding, 8.dpToPx(), horizontalPadding, 8.dpToPx())
                        }
                        contentContainer.addView(disclaimerTextView)
                    }
                }
                val orientationContainer = LinearLayout(context).apply {
                    orientation = if (isTablet) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                if (isTablet) {
                    orientationContainer.addView(contentContainer)
                    imageView?.let {
                        orientationContainer.addView(imageView, LinearLayout.LayoutParams(
                            150.dpToPx(),
                            150.dpToPx()
                        ).apply {
                            gravity = Gravity.TOP and Gravity.START
                            setMargins(0, 0, horizontalPadding, 0)
                        })
                    }
                } else {
                    imageView?.let {
                        orientationContainer.addView(imageView, LinearLayout.LayoutParams(
                            100.dpToPx(),
                            100.dpToPx()
                        ).apply {
                            gravity = Gravity.CENTER
                        })
                    }
                    orientationContainer.addView(contentContainer)
                }
                linearLayout.addView(orientationContainer)

                // Footer
                offer.footer?.forEach {
                    if (it.type == "text") {
                        var footerString: CharSequence? = null
                        val footerTextView = TextView(context).apply {
                            includeFontPadding = false
                            setTextSize(it.attributes?.size)
                            setTextColor(getTextColor(it.attributes?.appearance) ?: Color.GRAY)
                            setTextStyle(it.attributes?.emphasis)
                            setPadding(
                                horizontalPadding,
                                8.dpToPx(),
                                horizontalPadding,
                                16.dpToPx()
                            )
                        }
                        it.children?.let { children ->
                            children.forEach {
                                val text = android.text.SpannableString(it.text).apply {
                                    if (it.type == "link") clickableSpan(it.text) {
                                        context?.openLink(it.attributes?.to ?: "")
                                    }
                                }
                                footerString =
                                    android.text.TextUtils.concat(footerString ?: "", text)
                            }
                            footerTextView.text = footerString
                            footerTextView.movementMethod =
                                android.text.method.LinkMovementMethod.getInstance()
                            footerTextView.gravity = Gravity.END
                            linearLayout.addView(footerTextView)
                        }

                    }
                }
                container?.let {
                    container?.removeAllViews()
                    it.addView(parentContainer)
                }
            } ?: run {
                container?.removeAllViews()
            }
        }
    }

    private fun createCircle(color: Int): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.cornerRadii = floatArrayOf(100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f)
        shape.setColor(color)
        return shape
    }

    private fun TextView.setTextStyle(emphasis: String?) {
        emphasis?.let {
            when (it) {
                "bold" -> setTypeface(typeface, Typeface.BOLD)
                "italic" -> setTypeface(typeface, Typeface.ITALIC)
            }
        }
    }

    private fun TextView.setTextSize(size: String?) {
        textSize = when (size) {
            "extraSmall" -> 10f
            "small" -> 12f
            "large" -> 24f
            else -> 16f
        }
    }

    private fun getTextColor(color: String?): Int? {
        return when (color) {
            "accent" -> Color.WHITE
            "subdued" -> Color.parseColor("#585858")
            else -> null
        }
    }

    private fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    private fun horizontalPadding(isTablet: Boolean): Int =
        if (isTablet) 32.dpToPx() else 16.dpToPx()
}