package aurelienribon.bodyeditor.renderpanel.inputprocessors;

import aurelienribon.bodyeditor.AppManager;
import aurelienribon.bodyeditor.AssetsManager;
import aurelienribon.bodyeditor.models.ShapeModel;
import aurelienribon.bodyeditor.renderpanel.RenderPanel;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

/**
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ShapeCreationInputProcessor extends InputAdapter {
	boolean isActive = false;

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		boolean isValid = button == Buttons.LEFT && InputHelper.isShapeCreationKeyDown();

		if (!isValid)
			return false;
		isActive = true;

		List<ShapeModel> selectionShapes = AssetsManager.instance().getSelectedAsset().getShapes();
		ShapeModel lastShape = selectionShapes.isEmpty() ? null : selectionShapes.get(selectionShapes.size()-1);

		if (lastShape == null || lastShape.isClosed()) {
			lastShape = new ShapeModel();
			selectionShapes.add(lastShape);
		}

		if (lastShape.getVertices().size() >= 3 && AppManager.instance().nearestPoint == lastShape.getVertices().get(0)) {
			lastShape.close();
			AssetsManager.instance().getSelectedAsset().computePolygons();
			RenderPanel.instance().createBody();
		} else {
			Vector2 p = RenderPanel.instance().alignedScreenToWorld(x, y);
			lastShape.getVertices().add(p);
		}

		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (!isActive)
			return false;
		isActive = false;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (!isActive)
			return false;
		touchMoved(x, y);
		return true;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// Nearest point computation
		Vector2 p1 = RenderPanel.instance().screenToWorld(x, y);
		AppManager.instance().nearestPoint = null;

		List<ShapeModel> selectionShapes = AssetsManager.instance().getSelectedAsset().getShapes();
		ShapeModel shape = selectionShapes.isEmpty() ? null : selectionShapes.get(selectionShapes.size()-1);

		if (shape != null && !shape.isClosed() && shape.getVertices().size() >= 3)
			if (shape.getVertices().get(0).dst(p1) < 10 * RenderPanel.instance().getCamera().zoom)
				AppManager.instance().nearestPoint = shape.getVertices().get(0);

		// Next point assignment
		Vector2 p2 = RenderPanel.instance().alignedScreenToWorld(x, y);
		AppManager.instance().nextPoint = p2;
		return false;
	}
}
